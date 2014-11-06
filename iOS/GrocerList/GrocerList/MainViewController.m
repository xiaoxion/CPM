//
//  MainViewController.m
//  GrocerList
//
//  Created by Esau Rubio on 11/5/14.
//  Copyright (c) 2014 Strtatazima. All rights reserved.
//

#import "MainViewController.h"
#import "CRToast.h"
#import <Parse/Parse.h>

@interface MainViewController ()

@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self onRefreshData];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (data == nil) {
        return 0;
    } else {
        return data.count;
    }
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (data == nil || data.count == 0) {
        return nil;
    } else {
        UITableViewCell *simpleCell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:@"Cell"];
        if (simpleCell != nil) {
            PFObject *temp = data [indexPath.row];
            simpleCell.textLabel.text = temp[@"grocery"];
            simpleCell.detailTextLabel.text = [NSString stringWithFormat:@"%d", [[temp objectForKey:@"number"] intValue]] ;
        }
        
        return simpleCell;
    }
}

-(void)onRefreshData {
    PFQuery *query = [PFQuery queryWithClassName:@"Groceries"];
    [query findObjectsInBackgroundWithBlock:^(NSArray *objects, NSError *error) {
        if (!error) {
            [self onTableViewData:objects];
        } else {
            NSDictionary *options = @{
                                      kCRToastTextKey : [error userInfo],
                                      kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                      kCRToastBackgroundColorKey : [UIColor redColor],
                                      kCRToastAnimationInTypeKey : @(CRToastAnimationTypeGravity),
                                      kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeGravity),
                                      kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionLeft),
                                      kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionRight)
                                      };
            
            [CRToastManager showNotificationWithOptions:options completionBlock:nil];
        }
    }];
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return UITableViewCellEditingStyleDelete;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        [data[indexPath.row] deleteInBackground];
        [data removeObjectAtIndex:indexPath.row];
        [groceriesTable deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:true];
    }
}

-(void)onTableViewData:(NSArray*)incoming {
    data = incoming;
    groceriesTable.reloadData;
}

-(IBAction)onClick:(id)sender {
    UIButton *button = (UIButton*)sender;
    
    if (button.tag == 0) {
        UIAlertController *alertController = [UIAlertController
                                              alertControllerWithTitle:@"Are you Sure?"
                                              message:nil
                                              preferredStyle:UIAlertControllerStyleAlert];
        
        UIAlertAction *cancelAction = [UIAlertAction
                                       actionWithTitle:@"Cancel"
                                       style:UIAlertActionStyleCancel
                                       handler:^(UIAlertAction *action) {
                                           NSLog(@"Cancel");
                                       }];
        
        UIAlertAction *logoutAction = [UIAlertAction
                                    actionWithTitle:@"Log Out"
                                    style:UIAlertActionStyleDestructive
                                    handler:^(UIAlertAction *action) {
                                        [PFUser logOut];
                                        [self performSegueWithIdentifier:@"loggedOut" sender:self];
                                    }];
        
        [alertController addAction:logoutAction];
        [alertController addAction:cancelAction];
        
        [self presentViewController:alertController animated:YES completion:nil];
    } else if (button.tag == 1) {
        UIAlertController *alertController = [UIAlertController
                                              alertControllerWithTitle:@"Add Grocery"
                                              message:nil
                                              preferredStyle:UIAlertControllerStyleAlert];
        
        [alertController addTextFieldWithConfigurationHandler:^(UITextField *textField) {
             textField.placeholder = @"Grocery";
         }];
        
        [alertController addTextFieldWithConfigurationHandler:^(UITextField *textField) {
             textField.placeholder = @"#";
             textField.keyboardType = UIKeyboardTypeNumberPad;
         }];
        
        UIAlertAction *cancelAction = [UIAlertAction
                                       actionWithTitle:@"Cancel"
                                       style:UIAlertActionStyleCancel
                                       handler:^(UIAlertAction *action) {
                                           NSLog(@"Cancel");
                                       }];
        
        UIAlertAction *addAction = [UIAlertAction
                                   actionWithTitle:@"Add"
                                   style:UIAlertActionStyleDefault
                                   handler:^(UIAlertAction *action) {
                                       UITextField *grocery = alertController.textFields.firstObject;
                                       UITextField *rawNumber = alertController.textFields.lastObject;
                                       NSNumber *number = @([rawNumber.text intValue]);
                                       
                                       PFObject *gameScore = [PFObject objectWithClassName:@"Groceries"];
                                       gameScore[@"grocery"] = grocery.text;
                                       gameScore[@"number"] = number;
                                       gameScore.ACL = [PFACL ACLWithUser:[PFUser currentUser]];
                                       [gameScore saveInBackgroundWithBlock: ^(BOOL succeeded, NSError *error) {
                                           if (succeeded) {
                                               [self onRefreshData];
                                           } else {
                                               NSDictionary *options = @{
                                                                         kCRToastTextKey : [error userInfo],
                                                                         kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                                                         kCRToastBackgroundColorKey : [UIColor redColor],
                                                                         kCRToastAnimationInTypeKey : @(CRToastAnimationTypeGravity),
                                                                         kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeGravity),
                                                                         kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionLeft),
                                                                         kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionRight)
                                                                         };
                                               
                                               [CRToastManager showNotificationWithOptions:options completionBlock:nil];
                                           }
                                       }];
                                   }];
        
        [alertController addAction:cancelAction];
        [alertController addAction:addAction];
        
        [self presentViewController:alertController animated:YES completion:nil];
    }
}

@end
