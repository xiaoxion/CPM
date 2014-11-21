//
//  MainViewController.h
//  GrocerList
//
//  Created by Esau Rubio on 11/5/14.
//  Copyright (c) 2014 Strtatazima. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MainViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>
{
    IBOutlet UITableView *groceriesTable;
    NSMutableArray *data;
    NSString *path;
}

-(IBAction)onClick:(id)sender;

@end
